package com.kanawish.arcore.utils

import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Renderable

fun HitResult.buildAnchorNode() = AnchorNode(createAnchor())

fun HitResult.buildAnchorNode(child: Node) = AnchorNode(createAnchor()).apply {
    addChild(child)
}

fun Anchor.toNode() = AnchorNode(this)

fun Renderable.buildNode(x: Float = 0f, y: Float = 0f, z: Float = 0f) = Node().apply {
    renderable = this@buildNode
    localPosition = Vector3(x, y, z)
}
