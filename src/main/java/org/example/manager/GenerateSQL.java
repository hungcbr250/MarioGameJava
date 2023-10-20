package org.example.manager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class GenerateSQL {
    public static void main(String[] args) {
        Random random = new Random();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

//        for (int i = 1; i <= 100; i++) {
//            String ghichu = "ghi chu " + i;
//            String mahoadon = "HD" + i;
//            String ngaycapnhat = dateFormat.format(randomDate(random));
//            String ngaygiao = dateFormat.format(randomDate(random));
//            String ngaytao = dateFormat.format(randomDate(random));
//
//            String sql = "INSERT [djxuyen1_qa].[hoadon] ( [ghichu], [mahoadon], [ngaycapnhat], [ngaygiao], [ngaytao]) " +
//                    "VALUES ( N'" + ghichu + "', '" + mahoadon + "', '" + ngaycapnhat + "', '" + ngaygiao + "', '" + ngaytao + "' );";
//
//            System.out.println(sql);
//        }
        for(int i=6;i<70;i++){
            String ngaytao = dateFormat.format(randomDate(random));
            String sql="insert into hoadonchitiet(dongia,ngaytao,soluong,hoadonid,sanphamchitietid)\n" +
                    "VALUES ( 200000, '"+ ngaytao + "', '" + 3 + "', '" + i + "', 1 );";
            String sql2="insert into hoadonchitiet(dongia,ngaytao,soluong,hoadonid,sanphamchitietid\n"+
                    "values (20000,'"+ngaytao+"')";
            System.out.println(sql);
        }
    }

    // Hàm để tạo ngày tháng ngẫu nhiên trong khoảng từ ngày hôm nay đến 30 ngày sau
    private static Date randomDate(Random random) {
        long currentTime = System.currentTimeMillis();
        long randomTime = (long) (random.nextDouble() * 30 * 24 * 60 * 60 * 1000); // 30 days in milliseconds
        return new Date(currentTime + randomTime);
    }
}
