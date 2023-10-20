package org.example.jade;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Camera {
    private Matrix4f projectionMatrix, viewMatrix, inverseProjection, inverseView;
    public Vector2f position;

    private float projectionWidth = 6;
    private float projectionHeight = 3;
    public Vector4f clearColor = new Vector4f(1, 1, 1, 1);
    private Vector2f projectionSize = new Vector2f(projectionWidth, projectionHeight);

    private float zoom = 1.0f;

    public Camera(Vector2f position) {
        this.position = position;
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.inverseProjection = new Matrix4f();
        this.inverseView = new Matrix4f();
        adjustProjection();
    }

    // ma trận chiếu
    public void adjustProjection() {
//        thiết lập ma trận chiếu
        projectionMatrix.identity();
//        cấu hình ma trận
        projectionMatrix.ortho(0.0f, projectionSize.x * this.zoom,
                0.0f, projectionSize.y * zoom, 0.0f, 100.0f);
//        sau khi bạn đã chiếu một điểm từ không gian thế giới sang không gian chiếu bằng ma trận
//        projectionMatrix,bạn có thể sử dụng ma trận inverseProjection để chuyển đổi ngược trở lại
//        từ không gian chiếu sang không gian thế giới. Điều này thường được
//        thực hiện để xác định vị trí thế giới của các đối tượng khi người dùng tương tác với chúng trên màn hình.
        inverseProjection = new Matrix4f(projectionMatrix).invert();
    }

    // ma trận xem
    public Matrix4f getViewMatrix() {
//        . Camera này được giả định nhìn thẳng vào hướng -Z (theo trục Z âm).
        Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
//        vector chỉ hướng lên (theo trục Y).
        Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
//        khởi tạo ma trận xem bằng ma trận đơn vị.
        viewMatrix.identity();
//     viewMatrix.lookAt() là phương thức tạo ma trận xem dựa trên vị trí của camera,
//     vị trí mục tiêu mà camera nhìn vào (ở đây là (position.x, position.y, 20.0f)), và hướng lên (cameraUp).
        viewMatrix.lookAt(new Vector3f(position.x, position.y, 20.0f),
                cameraFront.add(position.x, position.y, 0.0f),
                cameraUp);
        inverseView = new Matrix4f(this.viewMatrix).invert();

        return this.viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Matrix4f getInverseProjection() {
        return this.inverseProjection;
    }

    public Matrix4f getInverseView() {
        return this.inverseView;
    }

    public Vector2f getProjectionSize() {
        return this.projectionSize;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public void addZoom(float value) {
        this.zoom += value;
    }
}
