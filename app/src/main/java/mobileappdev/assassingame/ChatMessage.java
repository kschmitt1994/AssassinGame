package mobileappdev.assassingame;

/**
 * Created by kennyschmitt on 3/22/17.
 */

public class ChatMessage {

    private String id;
    private String text;
    private String name;

    public ChatMessage(){
    }

    public ChatMessage (String text, String name){
        this.text = text;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
