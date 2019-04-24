package com.kanawish.arcore

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment.OnTapArPlaneListener
import com.google.ar.sceneform.ux.TransformableNode
import com.kanawish.arcore.utils.arPlaneTaps
import com.kanawish.arcore.utils.assignNewAnchorNode
import com.kanawish.arcore.utils.buildAnchorNode
import com.kanawish.arcore.utils.buildNode
import com.kanawish.arcore.utils.oneTimeInit
import com.kanawish.arcore.utils.singleModelRenderable
import com.kanawish.arcore.utils.singleViewRenderable
import com.kanawish.arcore.utils.trackedAugmentedImages
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.zipWith
import timber.log.Timber

class ArStashedCodeActivity : AppCompatActivity() {

    // Lazy ref to the ArCustomFragment instance.
    private val arFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArCustomFragment
    }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initAugmentedImageTracking()
    }
fun foo () {
    arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
        disposables += Singles.zip(
                singleModelRenderable(R.raw.andy),
                singleViewRenderable(R.layout.price_tag)
            )
            .subscribe { (andy, card) ->
            }
    }
}
    private fun initOnTapPlane() {
        disposables += Singles.zip(
                singleModelRenderable(R.raw.andy),
                singleViewRenderable(R.layout.price_tag)
            )
            .subscribe { (andy, card) ->
                disposables += arFragment.arPlaneTaps()
                    .subscribe { (hitResult, _, _) ->
                        arFragment.arSceneView.scene.addChild(
                                hitResult.buildAnchorNode(
                                        child = arFragment.buildLabeledTransformableNode(andy, card)
                                )
                        )
                    }
            }
    }

    private fun ArFragment.buildLabeledTransformableNode(
        model: ModelRenderable,
        label: ViewRenderable
    ) = TransformableNode(transformationSystem).apply {
        renderable = model
        select()
        addChild(label.buildNode(y = .25f))
    }

    private fun initializeOnTapPlane(andy: ModelRenderable) {
        disposables += arFragment.arPlaneTaps()
            .subscribe { (hitResult, _, _) ->
                arFragment.arSceneView.scene.addChild(
                        hitResult.buildAnchorNode(
//                                child = arFragment.buildAndyNode(andy)
                        )
                )
            }
    }


    fun initOnTapPlane(andyRenderable: ModelRenderable) {
        val tapListener = OnTapArPlaneListener { hitResult, plane, motionEvent ->
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            val andyNode = TransformableNode(arFragment.transformationSystem)
            andyNode.setParent(anchorNode)
            andyNode.renderable = andyRenderable
        }
        arFragment.setOnTapArPlaneListener(tapListener)
    }

    private fun initAugmentedImageTracking() {
        // This emits the first augmented image trackable we find.
        val firstAugmentedImage = arFragment.trackedAugmentedImages()
            .flatMap { images -> Observable.fromIterable(images) }
            .filter { it.index == 0 && it.trackingState == TrackingState.TRACKING }
            .firstElement()

        // This only adds the one time. Thinking the tracking will re-establish poosition?
        // TODO: Saw renderable perma-drop in tests, TBD but might be anchor got cleared somehow?
        disposables += singleModelRenderable(R.raw.damaged_helmet)
            .toMaybe()
            .map(ModelRenderable::assignNewAnchorNode)
            .zipWith(firstAugmentedImage)
            .subscribe { (anchorNode, augmentedImage) ->
                Timber.d("Augmented Image detected.")

                // Initializes internal anchor if needed.
                augmentedImage.oneTimeInit()

                // Assign the image's anchor to our node
                anchorNode.anchor = augmentedImage.anchors.first()

                // Update the scene
                arFragment.arSceneView.scene.update(augmentedImage.trackingState, anchorNode)
            }
    }

    private fun ArFragment.buildAndyNode(model: ModelRenderable) =
        TransformableNode(transformationSystem).apply {
            renderable = model
            select()
        }

}
