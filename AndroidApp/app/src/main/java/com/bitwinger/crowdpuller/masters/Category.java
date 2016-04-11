package com.bitwinger.crowdpuller.masters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nimesh on 06-02-2016.
 */
public class Category {
    private Integer Id;
    private String Code;
    private List<Category> childrens;

    public Category(Integer id, String code) {
        this.Id = id;
        this.Code = code;
        this.childrens = new ArrayList<Category>();
    }

    public Integer getId() {
        return Id;
    }

    public String getCode() {
        return Code;
    }

    public void addChild(Integer id, String code) {
        childrens.add(new Category(id, code));
    }

    public List<Category> getChildrens() {
        return childrens;
    }
}
