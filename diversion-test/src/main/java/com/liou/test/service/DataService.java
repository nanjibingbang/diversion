package com.liou.test.service;

import com.liou.test.entity.Param;
import com.liou.test.entity.Result;

public interface DataService {

    Result getData(Param type) throws RuntimeException;

}
