package com.github.oxal.spring.provider;

import com.github.oxal.provider.PackageProvider;

public class TinySpringProvider implements PackageProvider {
    @Override
    public String[] getPackages() {
        return new String[]{"com.github.oxal.spring"};
    }
}
