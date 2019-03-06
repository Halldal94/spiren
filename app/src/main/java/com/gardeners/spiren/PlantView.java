package com.gardeners.spiren;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlantView {
    private static final Random RANDOM = new Random();
    private static final int leafSparsity = 5;

    private final ModelRenderable leafRenderable;
    private final Node root;
    private final Node stalk;
    private final Node flower;
    private final List<Node> leaves;

    public PlantView(ModelRenderable leafRenderable, Node root, Node stalk, Node flower) {
        this.leafRenderable = leafRenderable;
        this.root = root;
        this.stalk = stalk;
        this.flower = flower;
        this.leaves = new ArrayList<>();
    }

    public void setHeight(int height) {
        float stalkHeight = height / 100.0F;
        float stalkThickness = 0.25F + height / 150.0F;
        float flowerSize = 0.25F + height / 150.0F;

        stalk.setLocalScale(new Vector3(stalkThickness, stalkHeight, stalkThickness));
        flower.setLocalPosition(new Vector3(0.0F, stalkHeight, 0.0F));
        flower.setLocalScale(new Vector3(flowerSize, flowerSize, flowerSize));

        int numLeaves = height / leafSparsity;

        // Add leaves, if any should be added
        for (int i = leaves.size(); i < numLeaves; i++) {
            Node leaf = new Node();
            leaf.setLocalRotation(Quaternion.axisAngle(new Vector3(0.0F, 1.0F, 0.0F), RANDOM.nextFloat() * 360.0F));
            leaf.setParent(root);
            leaf.setLocalPosition(new Vector3(0.0F, leafSparsity * (i + 0.5F) / 100.0F, 0.0F));
            leaf.setRenderable(leafRenderable);
            leaves.add(leaf);
        }

        // Remove leaves, if any should be removed
        for (int i = leaves.size() - 1; i > numLeaves; i--) {
            root.removeChild(leaves.remove(i));
        }

        // Update scale of all leaves
        for (int i = 0; i < leaves.size(); i++) {
            Node leaf = leaves.get(i);
            float scale = (height - i * leafSparsity) / 50.0F;
            leaf.setLocalScale(new Vector3(scale, scale, scale));
        }
    }
}
