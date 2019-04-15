package com.kanawish.arcore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.ar.core.AugmentedImageDatabase

import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment

/**
 * # Sceneform
 *
 * ## ArCustomFragment
 *
 * ArFragment is a Sceneform component. It's said to be the simplest way
 * to create a scene view.
 *
 * Compared to using ArCore directly, it takes care of a lot of things
 * on your behalf:
 *
 * - Setting up OpenGL, SurfaceView.
 * - Takes care of ArCoreApk installation/upgrade flow.
 * - Takes care of asking for required ArCore permissions. [CAMERA]
 *
 */
class ArCustomFragment : ArFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // NOTE: Convenient spot to turn on/off features as needed.
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    /**
     * We initialize the returned configuration here.
     *
     * Loads up augmented image DB, assigns it to session config.
     *
     * @return the configuration that will be used by ArFragment.
     */
    override fun getSessionConfiguration(session: Session): Config {
        return super.getSessionConfiguration(session).also { config ->
            config.augmentedImageDatabase = context
                ?.assets
                ?.open("good_images.imgdb")
                .let { inputStream ->
                    AugmentedImageDatabase.deserialize(session, inputStream)
                }

            // Normally off, causes issues with quality, but makes smaller images trackable.
            config.focusMode = Config.FocusMode.AUTO
        }
    }

}
