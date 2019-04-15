package com.kanawish.arcore.utils

import android.app.Activity
import androidx.annotation.LayoutRes
import androidx.annotation.RawRes
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Future

/*
 * @startuml
 * hide empty members
 *
 * Activity <|-- "<b>Activity</b>"
 * abstract class "<b>Activity</b>" << (F,orchid) RenderableUtils.kt>> {
 *  <i>singleViewRenderable(layoutId:Int):Single<ViewRenderable></i>
 *  <i>singleModelRenderable(rawId:Int):Single<ModelRenderable></i>
 * }
 *
 * class Future<T>
 * Future <|-- "<b>Future</b>"
 * abstract class "<b>Future</b>"<T> << (F,orchid) RenderableUtils.kt>> {
 *  <i>toSingle():Single<T></i>
 * }
 * @enduml
 *
 * @startuml
 * hide empty members
 *
 * abstract class Renderable
 * Renderable <|-- ViewRenderable
 * ViewRenderable : Builder.build():Future<ViewRenderable>
 * Renderable <|-- ModelRenderable
 * ModelRenderable : Builder.build():Future<ModelRenderable>
 * @enduml
 */

/**
 * Sceneform Renderable builders use `java.util.concurrent.Future`.
 * This is a small utility function to convert these `Future` instances
 * to RxJava friendly `Single` instances.
 */
private fun <T> Future<T>.toSingle() =
    Single.fromFuture(this)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

/**
 * Utility function that returns a Single<ViewRenderable>
 *
 * @param layout Layout res ID for the source View for our ViewRenderable
 * @return A Single<ViewRenderable> triggered on the main thread.
 */
fun Activity.singleViewRenderable(@LayoutRes layout: Int): Single<ViewRenderable> {
    return ViewRenderable
        .builder()
        .setView(this, layout)
        .build()
        .toSingle()
}

/**
 * Utility function that returns a Single<ModelRenderable>
 *
 * @param model Raw res ID for the source model for our ModelRenderable
 * @return A Single<ModelRenderable> triggered on the main thread.
 */
fun Activity.singleModelRenderable(@RawRes model: Int): Single<ModelRenderable> {
    return ModelRenderable
        .builder()
        .setSource(this, model)
        .build()
        .toSingle()
}

/**
 * Build a new AnchorNode auto-assigned to this Renderable
 *
 * @return AnchorNode that was assigned to this Renderable.
 */
fun Renderable.assignNewAnchorNode() : AnchorNode {
    return AnchorNode().also { newNode ->
        newNode.name = id.toString()
        newNode.renderable = this
    }
}
