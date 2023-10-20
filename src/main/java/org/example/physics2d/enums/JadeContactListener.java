package org.example.physics2d.enums;

import org.example.components.Component;
import org.example.jade.GameObject;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;

public class JadeContactListener implements ContactListener {
    //    bắt đầu va chạm
    @Override
    public void beginContact(Contact contact) {
        System.out.println("begin contact JadeContactListener ");
//        lấy thông tin của 2 đối tượng va chạm
        GameObject objA = (GameObject) contact.getFixtureA().getUserData();
        GameObject objB = (GameObject) contact.getFixtureB().getUserData();
//     Tạo một WorldManifold để lấy thông tin về hướng phản ứng của va chạm (normal)
//     trong không gian thế giới.
        WorldManifold worldManifold = new WorldManifold();
        contact.getWorldManifold(worldManifold);
//        hướng phản ứng của va chạm
        Vector2f aNormal = new Vector2f(worldManifold.normal.x, worldManifold.normal.y);
//        hướng ngược lại
        Vector2f bNormal = new Vector2f(aNormal).negate();
//để thông báo cho các thành phần rằng va chạm đã xảy ra và cung cấp thông tin cụ thể về va chạm, cho phép các
// thành phần này thực hiện các hành động tương ứng. Cụ thể, các thành phần có thể kiểm tra loại đối tượng va chạm
// (ví dụ: loại đối tượng là đất, quái vật, vũ khí, vv.) và thực hiện các xử lý phù hợp,
// ví dụ như xử lý sự va chạm với đất, giết chết quái vật, hoặc xử lý hiệu ứng khi va chạm.
        for (Component c : objA.getAllComponents()) {
            c.beginCollision(objB, contact, aNormal);
        }

        for (Component c : objB.getAllComponents()) {
            c.beginCollision(objA, contact, bNormal);
        }
    }

    //sau khi va chạm
    @Override
    public void endContact(Contact contact) {
        System.out.println("end contact JadeContactListener ");

        GameObject objA = (GameObject) contact.getFixtureA().getUserData();
        GameObject objB = (GameObject) contact.getFixtureB().getUserData();
        WorldManifold worldManifold = new WorldManifold();
        contact.getWorldManifold(worldManifold);
        Vector2f aNormal = new Vector2f(worldManifold.normal.x, worldManifold.normal.y);
        Vector2f bNormal = new Vector2f(aNormal).negate();

        for (Component c : objA.getAllComponents()) {
            c.endCollision(objB, contact, aNormal);
        }

        for (Component c : objB.getAllComponents()) {
            c.endCollision(objA, contact, bNormal);
        }
    }

    // trước khi va chạm được giải quyết
    @Override
    public void preSolve(Contact contact, Manifold manifold) {
        System.out.println("preSolve contact JadeContactListener ");
        // lấy ra đối tượng va chạm
        GameObject objA = (GameObject) contact.getFixtureA().getUserData();
        GameObject objB = (GameObject) contact.getFixtureB().getUserData();
        WorldManifold worldManifold = new WorldManifold();
//        lấy thông tin hướng va chạm
        contact.getWorldManifold(worldManifold);
        //        hướng của va chạm
        Vector2f aNormal = new Vector2f(worldManifold.normal.x, worldManifold.normal.y);
        //        hướng ngược lại
        Vector2f bNormal = new Vector2f(aNormal).negate();

        for (Component c : objA.getAllComponents()) {
            c.preSolve(objB, contact, aNormal);
        }

        for (Component c : objB.getAllComponents()) {
            c.preSolve(objA, contact, bNormal);
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {
        System.out.println("post contact JadeContactListener ");
        GameObject objA = (GameObject) contact.getFixtureA().getUserData();
        GameObject objB = (GameObject) contact.getFixtureB().getUserData();
        WorldManifold worldManifold = new WorldManifold();
        contact.getWorldManifold(worldManifold);
//        hướng của va chạm
        Vector2f aNormal = new Vector2f(worldManifold.normal.x, worldManifold.normal.y);
//        hướng ngược lại
        Vector2f bNormal = new Vector2f(aNormal).negate();

        for (Component c : objA.getAllComponents()) {
            c.postSolve(objB, contact, aNormal);
        }

        for (Component c : objB.getAllComponents()) {
            c.postSolve(objA, contact, bNormal);
        }
    }
}
