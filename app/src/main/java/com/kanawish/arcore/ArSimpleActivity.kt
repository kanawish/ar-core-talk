package com.kanawish.arcore

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.kanawish.arcore.utils.checkIsSupportedDeviceOrFinish
import com.kanawish.arcore.utils.toSingle
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

class ArSimpleActivity : AppCompatActivity() {

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

        // Load the fancy Andy 3D model
        disposables +=
        singleModelRenderable("andy-machinery.sfb").subscribe { andy ->
            initTouchPlacement(andy)
        }

    }

    private fun singleModelRenderable(assetFileName: String) = ModelRenderable
        .builder()
        .setSource(
                this,
                Uri.parse("file:///android_asset/$assetFileName")
        )
        .build()
        .toSingle()

    private fun initTouchPlacement(model: ModelRenderable) {
        arFragment.setOnTapArPlaneListener { hitResult, plane, _ ->
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            // The node for the model itself.
            val modelNode = TransformableNode(arFragment.transformationSystem)
            modelNode.renderable = model
            modelNode.setParent(anchorNode)
        }

    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

}