package wlj.com.coolweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import wlj.com.coolweather.db.City;
import wlj.com.coolweather.db.County;
import wlj.com.coolweather.db.Province;
import wlj.com.coolweather.util.HttpUtil;
import wlj.com.coolweather.util.JsonUtil;

/**
 * 省市区级联的碎片
 */
public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView chooseAreaTitle;
    private Button chooseAreaBackBtn;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    //省市区列表
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    //选中的省市
    private Province selectedProvince;
    private City selectedCity;
    //当前选中的级别
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        chooseAreaTitle = view.findViewById(R.id.choose_area_title_text);
        chooseAreaBackBtn = view.findViewById(R.id.choose_area_back_btn);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (currentLevel == LEVEL_PROVINCE) {
                selectedProvince = provinceList.get(position);
                queryCities();
            } else if (currentLevel == LEVEL_CITY) {
                selectedCity = cityList.get(position);
                queryCounties();
            }
        });
        chooseAreaBackBtn.setOnClickListener(v -> {
            if (currentLevel == LEVEL_COUNTY) {
                queryCities();
            } else if (currentLevel == LEVEL_CITY) {
                Log.d("ChooseAreaFragment", "开始查询省");
                queryProvinces();
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查，查不到再通过http请求
     */
    public void queryProvinces() {
        chooseAreaTitle.setText("中国");
        chooseAreaBackBtn.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            Log.d("ChooseAreaFragment", "查询省");
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    public void queryCities() {
        chooseAreaTitle.setText(selectedProvince.getProvinceName());
        chooseAreaBackBtn.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid=?",
                String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    public void queryCounties() {
        chooseAreaTitle.setText(selectedCity.getCityName());
        chooseAreaBackBtn.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid=?", String.valueOf(selectedCity.getId()))
                .find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String addrerss = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(addrerss, "county");
        }
    }

    private void queryFromServer(String address, final String queryType) {
        showProgressDialog();
        HttpUtil.doOkHttpRequest(address, new Callback() {
            /**
             * 失败回调
             * @param call
             * @param e
             */
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread回到主线程处理逻辑
                getActivity().runOnUiThread(() -> {
                    closeProgressDialog();
                    Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                });
            }

            /**
             * 成功回调
             * @param call
             * @param response
             * @throws IOException
             */
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean result = false;
                if (response.body() != null) {
                    String responseStr = response.body().toString();
                    if ("province".equals(queryType)) {
                        result = JsonUtil.parseProvinceResponse(responseStr);
                    } else if ("city".equals(queryType)) {
                        result = JsonUtil.parseCityResponse(responseStr, selectedProvince.getId());
                    } else {
                        result = JsonUtil.parseCountyResponse(responseStr, selectedCity.getId());
                    }
                }
                if (result) {
                    getActivity().runOnUiThread(() -> {
                        closeProgressDialog();
                        if ("province".equals(queryType)) {
                            queryProvinces();
                        } else if ("city".equals(queryType)) {
                            queryCities();
                        } else {
                            queryCounties();
                        }
                    });
                }

            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
