package com.jinmifood.jinmi.common.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReservedKeywords {

    public static final List<String> RESERVED_LIST = Collections.unmodifiableList(Arrays.asList(
            "admin", "administrator", "system", "root", "super", "관리자", "운영자",
            "master", "webmaster", "support", "cs", "ceo","시발","병신","ㅈ병신","ㅅ발","시발롬","좆까","시발련","시발년","개새끼","개병신","새끼","십새끼"

    ));

    private ReservedKeywords() {
    }
}
