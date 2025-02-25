package com.dsv.datafactory.file.extraction.processor.models;

import com.dsv.datafactory.model.Vertices;
import lombok.Setter;

import java.util.ArrayList;
// Warto dodać  @Setter/Getter z Lombok'a jeśli chce jawny zapis do każdej ze zmiennej
@Setter
public class BoundingPoly {
    // klasa będzie bardziej elastyczna na przyszłe zmiany implementacji.
    //private List<Vertices> vertices;
    //private List<NormalizedVertices> normalizedVertices;
    private ArrayList<Vertices> vertices;
    private ArrayList<NormalizedVertices> normalizedVertices;

    // Może warto dodać??
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

    // Lista błędów w metodzie
    // Błąd arytmetyczny: int x = vertex.getX()/width ->  dzielenie całkowitoliczbowe -> double
    // Brak przypisania wyniku: normalized
    // życie złego typu: Vertices -> może powinniśmy użyć NormalizedVertices
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
        // Czy nie powinniśmy użyć innego typu?
        // this.normalizedVertices = normalized;
    }

    // Warto obsłużyć
    @Override
    public String toString() {
        return "BoundingPoly{" +
                "vertices=" + vertices +
                ", normalizedVertices=" + normalizedVertices +
                '}';
    }
}
