package run.tere.bot.speakers;

public class Style {
    private String name;
    private int id;
    private String type;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Style{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", type='" + type + '\'' +
                '}';
    }
}

