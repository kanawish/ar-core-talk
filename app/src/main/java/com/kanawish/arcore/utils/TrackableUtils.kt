package com.kanawish.arcore.utils

import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState

/**
 * This will add an anchor using Augmented Image's centerPose.
 *
 * Only applies on the first call, no-ops otherwise.
 */
fun AugmentedImage.oneTimeInit() {
    // TODO: Check this is really independent of TRACKING...
    // If we don't have an anchor already for the image, we create it.
    if (anchors.isEmpty() && trackingState == TrackingState.TRACKING) {
        // If not, we create one, with the default center pose.
        createAnchor(centerPose)
    }
}

/*

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

@startuml
hide empty members
namespace arcore {
}
namespace sceneform {
}
@enduml

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
}

arcore.Anchor -down[hidden]- sceneform.TransformChangedListener

@enduml

@startuml
@enduml

 */
