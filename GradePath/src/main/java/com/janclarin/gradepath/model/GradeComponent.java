package com.janclarin.gradepath.model;

public class GradeComponent extends DatabaseItem {

    private long courseId;
    private String name;
    private double weight;
    private int numberOfItems;

    private double componentAverage;

    public GradeComponent() {
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    public void setNumberOfItems(int numberOfItems) {
        this.numberOfItems = numberOfItems;
    }

    public double getComponentAverage() {
        return componentAverage;
    }

    public void setComponentAverage(double componentAverage) {
        this.componentAverage = componentAverage;
    }

    /**
     * @return component average as a String representation.
     */
    public String getComponentAverageString() {
        return Double.toString(componentAverage) + "/" + Double.toString(weight);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof GradeComponent)) return false;

        GradeComponent other = (GradeComponent) object;

        return (this.id == other.id);
    }
}
