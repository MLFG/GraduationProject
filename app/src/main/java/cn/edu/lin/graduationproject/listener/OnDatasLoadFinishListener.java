package cn.edu.lin.graduationproject.listener;

import java.util.List;

/**
 * Created by liminglin on 17-2-28.
 */

public interface OnDatasLoadFinishListener<T> {

    void onLoadFinish(List<T> datas);
}
