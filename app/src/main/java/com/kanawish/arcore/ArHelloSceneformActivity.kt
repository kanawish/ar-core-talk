package com.kanawish.arcore

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.NodeParent
import com.google.ar.sceneform.rendering.ModelRenderable
import com.kanawish.arcore.utils.assignNewAnchorNode
import com.kanawish.arcore.utils.checkIsSupportedDeviceOrFinish
import com.kanawish.arcore.utils.oneTimeInit
import com.kanawish.arcore.utils.singleModelRenderable
import com.kanawish.arcore.utils.trackedAugmentedImages
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.zipWith
import timber.log.Timber

class ArHelloSceneformActivity : AppCompatActivity() {

    // Lazy ref to the ArCustomFragment instance.
    val arFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArCustomFragment
    }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If device is not supported, early return.
        if (!checkIsSupportedDeviceOrFinish()) return

        // `FrameLayout` holding a `ArCustomFragment`
        setContentView(R.layout.activity_main)

        // We'll anchor the solar system model to an Augmented Image.
        val solarNode = singleModelRenderable(R.raw.damaged_helmet)
            .map(ModelRenderable::assignNewAnchorNode)
            .toObservable()

        // TODO: Validate what happens when
        val firstTrackable = arFragment.trackedAugmentedImages()
            .flatMap { images -> Observable.fromIterable(images) }
            .filter { it.index == 0 && it.trackingState == TrackingState.TRACKING }
            .firstElement()

        // This only adds the one time. Thinking the tracking will re-establish poosition?
        // TODO: Saw renderable perma-drop in tests, TBD but might be anchor got cleared somehow?
        disposables += singleModelRenderable(R.raw.damaged_helmet)
            .toMaybe()
            .map(ModelRenderable::assignNewAnchorNode)
            .zipWith(firstTrackable)
            .subscribe { (anchorNode, augmentedImage) ->
                Timber.d("Augmented Image detected.")

                // Initializes internal anchor if needed.
                augmentedImage.oneTimeInit()

                // Assign the image's anchor to our node
                anchorNode.anchor = augmentedImage.anchors.first()

                // Update the scene
                arFragment.arSceneView.scene.update(augmentedImage.trackingState,anchorNode)
            }

        // TODO: Implement
//        disposables += singleModelRenderable(R.raw.andy)
//            .subscribe { curiosity ->
//                arFragment.bindScenery(curiosity)
//            }
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }


}

/**
 * Depending on tracking state,
 */
fun NodeParent.update(trackingState: TrackingState, anchorNode: Node) {
    when( trackingState ) {
        TrackingState.TRACKING -> {
            // Can we find our anchorNode in the scene? If not, we'll need to add it.
            if( findByName(anchorNode.name) == null ) {
                addChild(anchorNode)
            }
        }
        TrackingState.STOPPED -> {
            // Remove from scene when tracking stopped.
            findByName(anchorNode.name)?.let(::removeChild) ?:
                    Timber.w("STOPPED state, but could not find our anchorNode in the scene.")
        }
        TrackingState.PAUSED -> Timber.d("When an image is in PAUSED state, but the camera is not PAUSED, it has been detected, but not yet tracked.")
    }
}
