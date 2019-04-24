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
        singleModelRenderable(R.raw.andy03).subscribe { goldy ->
            initTouchPlacement(goldy)
        }

    }

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