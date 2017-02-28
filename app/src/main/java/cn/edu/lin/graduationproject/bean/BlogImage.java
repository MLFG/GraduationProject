package cn.edu.lin.graduationproject.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * 朋友圈图片的实体类
 * Created by liminglin on 17-2-28.
 */

@DatabaseTable
public class BlogImage{
    @DatabaseField(id=true)
    String imgUrl;
    @DatabaseField
    String bitmap; // Bitmap 图形 -- 打散 -- byte[] -- BASE64编码 -- String

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setBitmap(String bitmap) {
        this.bitmap = bitmap;
    }

    public String getImgUrl() {

        return imgUrl;
    }

    public String getBitmap() {
        return bitmap;
    }
}
