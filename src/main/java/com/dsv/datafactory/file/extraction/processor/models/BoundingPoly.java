package com.dsv.datafactory.file.extraction.processor.models;

import com.dsv.datafactory.model.Vertices;
import lombok.Setter;

import java.util.ArrayList;
// It's worth adding @Setter/Getter from Lombok if you want explicit write to each variable
@Setter
public class BoundingPoly {
    // the class will be more flexible for future implementation changes.
//private List<Vertices> vertices;
//private List<NormalizedVertices> normalizedVertices;
    private ArrayList<Vertices> vertices;
    private ArrayList<NormalizedVertices> normalizedVertices;

// Maybe it's worth adding??
/*public BoundingPoly() {
this.vertices = new ArrayList<>();
this.normalizedVertices = new ArrayList<>();
}*/

    public ArrayList<NormalizedVertices> getNormalizedVertices() {
        return normalizedVertices;
    }

    public ArrayList<Vertices> getVertices() {
        return vertices;
    }

    // List of errors in the method
// Arithmetic error: int x = vertex.getX()/width -> integer division -> double
// No result assignment: normalized
// bad type life: Vertices -> maybe we should use NormalizedVertices
    public void normalizeVertices(int width, int height) {
        if (vertices == null || vertices.isEmpty() || width == 0 || height == 0) {
            return;
        }
        List<NormalizedVertices> normalized = new ArrayList<>();
        for (Vertices vertex : this.vertices) {
            double x = (double) vertex.getX() / width;
            double y = (double) vertex.getY() / height;
            normalized.add(new NormalizedVertices(x, y));
        }
// Shouldn't we use a different type?
// this.normalizedVertices = normalized;
    }

    // Worth serving - nice equals?
    @Override
    public String toString() {
        return "BoundingPoly{" +
                "vertices=" + vertices +
                ", normalizedVertices=" + normalizedVertices +
                '}';
    }
}
