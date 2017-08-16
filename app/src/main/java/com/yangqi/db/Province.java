package com.yangqi.db;

import org.litepal.crud.DataSupport;

/**
 * 省
 */

public class Province extends DataSupport{
    /**
     * 字段 id
     */
    private int id;
    /**
     * 省份名字
     */
    private String provinceName;
    /**
     * 省份代码
     */
    private int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
