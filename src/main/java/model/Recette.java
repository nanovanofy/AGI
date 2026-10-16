package model;

/**
 * Une recette de la table « recette ».
 * Les ingrédients sont stockés en base sous forme de texte séparé par des virgules,
 * par exemple « oeufs,tomates,fromage râpé ».
 */
public class Recette {

    private int id;
    private String titre;
    private String ingredients;
    private String instructions;
    private int tempsMinutes;

    public Recette() {
    }

    public Recette(int id, String titre, String ingredients, String instructions, int tempsMinutes) {
        this.id = id;
        this.titre = titre;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.tempsMinutes = tempsMinutes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getTempsMinutes() {
        return tempsMinutes;
    }

    public void setTempsMinutes(int tempsMinutes) {
        this.tempsMinutes = tempsMinutes;
    }
}
