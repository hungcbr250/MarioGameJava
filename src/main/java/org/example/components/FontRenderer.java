package org.example.components;

public class FontRenderer extends Component {
    public FontRenderer(){
        gameObject =null;
    }
    @Override
    public void start(){
        if(gameObject.getComponent(SpriteRenderer.class)!= null){
            System.out.println("Found font renderer");
        }
    }

    @Override
    public void update(float dt) {

    }


}
