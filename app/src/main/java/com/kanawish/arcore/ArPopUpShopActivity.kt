package com.kanawish.arcore

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.NodeParent
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.FixedWidthViewSizer
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.kanawish.arcore.DummyAppState.storeInventory
import com.kanawish.arcore.utils.checkIsSupportedDeviceOrFinish
import com.kanawish.arcore.utils.singleModelRenderable
import com.kanawish.arcore.utils.singleViewRenderable
import com.kanawish.arcore.utils.trackedAugmentedImages
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Maybes
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

/* App structure diagram
@startuml
hide empty members
class Activity
class ArPopUpShopActivity
object activity_main.xml
Activity <|-d- ArPopUpShopActivity
ArPopUpShopActivity *-- ArFragment
ArPopUpShopActivity *.. activity_main.xml
skinparam backgroundcolor #2C2C2C
skinparam class {

ArrowColor Tomato
BorderColor Tomato
}
skinparam object {
ArrowColor Tomato
BorderColor Tomato
}
@enduml
note right of ArPopUpShopActivity : A regular Activity
 */

class ArPopUpShopActivity : AppCompatActivity() {

    // Lazy ref to the ArCustomFragment instance.
    private lateinit var arFragment: ArCustomFragment

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If device is not supported, early return.
        if (!checkIsSupportedDeviceOrFinish()) return

        // `FrameLayout` holding a `ArCustomFragment`
        setContentView(R.layout.activity_main)
        arFragment = supportFragmentManager
            .findFragmentById(R.id.ux_fragment) as ArCustomFragment

        // Load the Andy 3D model
        disposables +=
        singleViewRenderable(R.layout.selected_card_view).subscribe { selected ->
            initializeStore(selected)
        }

        initAugmentedImageTracking()
    }

    val colors = listOf(
            Vector3(0.92f,0.0f,0.0f),
            Vector3(0.52f,0.50f,0.10f),
            Vector3(0.0f,0.90f,0.10f),
            Vector3(0.0f,0.50f,0.60f),
            Vector3(0.02f,0.0f,0.9f)
    )

    fun initializeStore(selectedView: ViewRenderable) {

        // The "selected UI" is a single shared node, assigned a different parent on tap detection.
        selectedView.sizer = FixedWidthViewSizer(0.2f)
        selectedView.isShadowCaster = false
        val selectedNode = Node().apply {
            renderable = selectedView
        }

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            selectedNode.setParent(null)

            val mod = storeInventory.size % colors.size

            Singles.zip(
//                    singleModelRenderable(R.raw.andy),
                    singleModelRenderable(R.raw.andy02),
                    singleViewRenderable(R.layout.price_tag)
            )
                .subscribe { (customAndy, priceTag) ->
                    customAndy.getMaterial().setFloat3("andyColor", colors[mod])
                    val modelNode = hitResult.createAnchor()
                        .buildAndyInstance(
                                customAndy,
                                selectedNode
                        )

                    storeInventory[modelNode] = AndroidMiniState(
                            name = "Item ${storeInventory.size + 1}",
                            description = "Mini Collectible ${storeInventory.size + 1}",
                            price = 3 + (3 * Math.random()).toInt()
                    )

                    // Will occur once the new card view instance is built.
                    storeInventory[modelNode]?.also { newMini ->
                        priceTag.view.findViewById<TextView>(R.id.priceLabel).text =
                            "${newMini.price}$"
                    }

                    // A permanent label node - using Renderable.buildNode()
                    val priceTagNode = Node()
                    priceTag.isShadowCaster = false
                    priceTag.sizer = FixedWidthViewSizer(0.1f) // 10cm max width
                    priceTagNode.renderable = priceTag
                    priceTagNode.localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), -90f)
                    priceTagNode.localPosition = Vector3(0f, 0.02f, 0.2f)
                    modelNode.addChild(priceTagNode)
                }

        }
    }

    fun Anchor.buildAndyInstance(model: ModelRenderable, selectedNode: Node): TransformableNode {

        // The root spot for the new Andy instance.
        val anchorNode = AnchorNode(this)
        anchorNode.setParent(arFragment.arSceneView.scene)

        // The node for the model itself.
        val modelNode = TransformableNode(arFragment.transformationSystem)
        modelNode.setParent(anchorNode)
        modelNode.renderable = model

        modelNode.setOnTapListener { _, _ ->
            modelNode.select() // Self-select, since overriding listener removes the original behaviour.
            (selectedNode.renderable as? ViewRenderable)?.apply {
                storeInventory[modelNode]?.also { state ->
                    // TODO: Consider extending Node
                    // NOTE: Not to be confused with Activity's findViewById<>!
                    view.findViewById<TextView>(R.id.description).text = "${state.description}"
                    view.findViewById<TextView>(R.id.priceLabel).text = "${state.price} $"
                    view.findViewById<Button>(R.id.favoriteButton).setOnClickListener {
                        storeInventory[modelNode] = state.copy(faved = true)
                        arFragment.transformationSystem.selectNode(null)
                        selectedNode.setParent(null)
                    }
                    view.findViewById<Button>(R.id.addCartButton).setOnClickListener {
                        storeInventory[modelNode] = state.copy(inCart = true)
                        arFragment.transformationSystem.selectNode(null)
                        selectedNode.setParent(null)
                    }
                }
            }
            selectedNode.setParent(modelNode)
            selectedNode.localPosition = Vector3(0f,0.25f,0f)
            selectedNode.localRotation = modelNode.localRotation
        }

        return modelNode
    }

    private fun initAugmentedImageTracking() {
        // This emits tracked augmented images.
        val trackedAugmentedImage = arFragment.trackedAugmentedImages()
            .flatMap { images -> Observable.fromIterable(images) }
            .filter { it.index == 0 && it.trackingState == TrackingState.TRACKING }


        // This only adds the one time. Thinking the tracking will re-establish poosition?
        // TODO: Saw renderable perma-drop in tests, TBD but might be anchor got cleared somehow?
        disposables += Maybes
            .zip(
                singleViewRenderable(R.layout.store_banner_view).toMaybe(),
                singleModelRenderable(R.raw.andy03).toMaybe(),
                trackedAugmentedImage.firstElement()
            )
            .subscribe { (viewRenderable, andy03, augmentedImage) ->
                Timber.d("Augmented Image detected.")

                // Init View Renderable
                viewRenderable.apply {
                    isShadowCaster = false
                    isShadowReceiver = false
                    sizer = FixedWidthViewSizer(1f)
                }

                // Init an Augmented Image Anchor.
                augmentedImage.createAnchor(augmentedImage.centerPose)

                // Create AnchorNode with augmented image's anchor.
                val anchorNode = AnchorNode(augmentedImage.anchors.first())

                // Assign Renderable to a node 60cm above the augmented image anchor.
                val storefrontNode = Node().apply {
                    renderable = viewRenderable
                    localPosition = Vector3(0f,0.4f,-1f)
                }

                // Assign store UX ndoe to the anchor node.
                anchorNode.addChild(storefrontNode)

                val goldyNode = Node().apply {
                    renderable = andy03
                }
//                anchorNode.addChild(goldyNode)

                // Add Anchor Node to the scene.
                arFragment.arSceneView.scene.addChild(anchorNode)
            }
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

}

/**
 * NOTE: To validate, but this is mostly good for adding and cleanup, disabled nodes don't show up in graph.
 */
fun NodeParent.update(trackingState: TrackingState, anchorNode: Node) {
    when (trackingState) {
        TrackingState.TRACKING -> {
            // Can we find our anchorNode in the scene? If not, we'll need to add it.
            if (findByName(anchorNode.name) == null) {
                addChild(anchorNode)
            }
        }
        TrackingState.STOPPED -> {
            // Remove from scene when tracking stopped.
            findByName(anchorNode.name)?.let(::removeChild)
                ?: Timber.w("STOPPED state, but could not find our anchorNode in the scene.")
        }
        TrackingState.PAUSED -> Timber.d("When an image is in PAUSED state, but the camera is not PAUSED, it has been detected, but not yet tracked.")
    }
}
