package com.kanawish.arcore.utils

import android.view.MotionEvent
import com.google.ar.core.AugmentedImage
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.ux.ArFragment
import io.reactivex.Observable
import timber.log.Timber


/*
    Planes Diagram
    - Outlines `Plane` types
    - Outlines where touch events are processed.

@startuml

hide empty members

namespace arcore {
class Plane
enum Type {
HORIZONTAL_DOWNWARD_FACING
HORIZONTAL_UPWARD_FACING
VERTICAL
}
Plane o- Type
}

namespace sceneform {
class ArSceneView
class BaseArFragment {
+onPeekTouch(htr,me)
+setOnTapArPlaneListener(l)
}
class ArFragment
BaseArFragment *-right- ArSceneView
BaseArFragment <|-- ArFragment
}

arcore.Plane .[hidden]. sceneform.BaseArFragment

@enduml
*/

/**
 * @param hitResult The ARCore hit result that occurred when tapping the plane.
 * @param plane The ARCore Plane that was tapped.
 * @param motionEvent The motion event that triggered the tap.
 */
data class PlaneTap(val hitResult: HitResult, val plane:Plane, val motionEvent:MotionEvent)

/**
 * Returns an observable of `HitResult` for the parent ArFragment.
 *
 * WARNING: Due to ArFragment not exposing a getter for tap listener,
 * calling this will silently disable previous subscriptions. Make sure
 * to dispose of any existing subscriptions _before_ starting a new one.
 *
 * You can use ConnectableObservable APIs if you need to multiple subscriptions
 * running on this source.
 */
fun ArFragment.arPlaneTaps():Observable<PlaneTap> {
    return Observable.create { emitter ->
        setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            emitter.onNext(PlaneTap(hitResult, plane, motionEvent))
        }
        emitter.setCancellable { setOnTapArPlaneListener(null) }
    }
}


/**
 * This builds an Observable that will return collections of augmented images
 * when the camera associated with the ArFrame is in a TRACKING state.
 *
 * Each AugmentedImage has it's own Tracking state you'll need to contend with.
 *
 * @return Observable<Collection<AugmentedImage>> from ArCore
 */
fun ArFragment.trackedAugmentedImages() : Observable<Collection<AugmentedImage>> {
    return Observable.create { emitter ->
        // Listener that emits updated Collection<AugmentedImage> when camera is tracking.
        val listener = Scene.OnUpdateListener {
            arSceneView.arFrame?.run {
                if( camera.trackingState == TrackingState.TRACKING) {
                    getUpdatedTrackables(AugmentedImage::class.java).let { trackables ->
                        if(trackables.isNotEmpty()) {
                            var x = ""
                            trackables.forEach {
                                x += "${it.index}[${it.trackingState}]   "
                            }
                            Timber.d("trackedAugmentedImages $x")
                            emitter.onNext(trackables)
                        }
                    }
                }
            }
        }

        // Add listener to scene.
        Timber.d("trackedAugmentedImages.addOnUpdateListener()")
        arSceneView.scene.addOnUpdateListener(listener)

        // Clean up and remove listener.
        emitter.setCancellable {
            Timber.d("trackedAugmentedImages.removeOnUpdateListener()")
            arSceneView.scene.removeOnUpdateListener(listener)
        }
    }
}

