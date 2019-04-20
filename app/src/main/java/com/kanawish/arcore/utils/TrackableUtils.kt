package com.kanawish.arcore.utils

import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState
import timber.log.Timber

/**
 * This will add an anchor using Augmented Image's centerPose.
 *
 * Only applies on the first call, no-ops otherwise.
 */
fun AugmentedImage.oneTimeInit() {
    // TODO: Check this is really independent of TRACKING...
    if( trackingState != TrackingState.TRACKING ) {
        Timber.w("Att")
    }
    // If we don't have an anchor already for the image, we create it.
    if (anchors.isEmpty()) {
        // If not, we create (and attach) one, with the default center pose.
        createAnchor(centerPose)
    }
}

/* Trackable inheritance diagram
@startuml
hide empty members

namespace arcore {
    interface Trackable {
        +anchors
        +trackingState
        +createAnchor(pose:Pose)
    }
    class AugmentedFace
    class AugmentedImage
    class Plane
    class Point

    Trackable <|-l- AugmentedFace
    Trackable <|-d- AugmentedImage
    Trackable <|-d- Plane
    Trackable <|-r- Point
}
@enduml
*/

/* ArCore packages
@startuml
hide empty members
namespace arcore {
}
namespace sceneform {
}
@enduml
*/

/* Tracking state and Nodes Diagram
@startuml
hide empty members

namespace arcore {

    interface Trackable {
     +trackingState
     +createAnchor(pose:Pose)
    }

    class Anchor {
     +detach()
     +cloudAnchorId
     +pose
    }

    Anchor *-up- TrackingState

    enum TrackingState {
     PAUSED
     STOPPED
     TRACKING
    }

    Trackable *-l- TrackingState
    Trackable *-d- Anchor

    note right of Anchor
    Describes a fixed location
    and orientation in the real
    world.
    end note

}

note top of arcore.TrackingState
STOPPED is final
end note

namespace sceneform {
    class Node
    interface LifecycleListener
    interface TransformChangedListener
    interface OnTapListener
    interface OnTouchListener
    Node o-u- TransformChangedListener
    Node o-u- LifecycleListener
    Node o-r- OnTapListener
    Node o-l- OnTouchListener

    class AnchorNode
    Node <|-- AnchorNode

    class TranformableNode
    Node <|-- TranformableNode
}

arcore.Anchor -down[hidden]- sceneform.TransformChangedListener

@enduml

@startuml

class Node
note right of Node : We'll cover this in a sec.

@enduml

 */
