package com.kanawish.arcore.utils

import android.app.Activity
import androidx.annotation.LayoutRes
import androidx.annotation.RawRes
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
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
 * namespace utils {
 * Activity <|-- "<b>Activity</b>"
 * abstract class "<b>Activity</b>" << (F,orchid) RenderableUtils.kt>> {
 *  <i>singleViewRenderable(layoutId:Int):Single</i>
 *  <i>singleModelRenderable(rawId:Int):Single</i>
 * }
 *
 * class Future<T>
 * Future <|-- "<b>Future</b>"
 * abstract class "<b>Future</b>"<T> << (F,orchid) RenderableUtils.kt>> {
 *  <i>toSingle():Single<T></i>
 * }
 * }
 * @enduml
 *
 * @startuml
 * hide empty members
 * namespace sceneform {
 * abstract class Renderable
 * Renderable <|-- ViewRenderable
 * ViewRenderable : Builder.build():Future
 * Renderable <|-- ModelRenderable
 * ModelRenderable : Builder.build():Future
 * }
 * @enduml
 */

/**
 * Sceneform Renderable builders use `java.util.concurrent.Future`.
 * This is a small utility function to convert these `Future` instances
 * to RxJava friendly `Single` instances.
 */
fun <T> Future<T>.toSingle() =
    Single.fromFuture(this)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun foo(): Single<ViewRenderable> {
    return ViewRenderable
        .builder()
        .build()
        .toSingle()
}

/**
 * Utility function that returns a Single<ViewRenderable>
 *
 * @param layout Layout res ID for the source View for our ViewRenderable
 * @return A Single<ViewRenderable> triggered on the main thread.
 */
fun Activity.singleViewRenderable(@LayoutRes layout: Int) =
    ViewRenderable
        .builder()
        .setView(this, layout)
        .build()
        .toSingle()

/**
 * Utility function that returns a Single<ModelRenderable>
 *
 * @param model Raw res ID for the source model for our ModelRenderable
 * @return A Single<ModelRenderable> triggered on the main thread.
 */
fun Activity.singleModelRenderable(@RawRes model: Int) =
    ModelRenderable
        .builder()
        .setSource(this, model)
        .build()
        .toSingle()

/**
 * Build a new AnchorNode auto-assigned to this Renderable
 *
 * @return AnchorNode that was assigned to this Renderable.
 */
fun <T : Renderable> T.assignNewAnchorNode() = AnchorNode().apply {
        name = id.toString()
        renderable = this@assignNewAnchorNode
    }
