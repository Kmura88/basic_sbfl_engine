package basic_sbfl_engine.sbfl;

import java.util.ArrayList;
import java.util.List;

import basic_sbfl_engine.coverage.CoverageMatrix;
import basic_sbfl_engine.data.Suspiciousness;

public abstract class SpectrumBasedFL {

    protected abstract double formula(double ef, double nf, double ep, double np);

    public List<Suspiciousness> compute(CoverageMatrix matrix) {
        List<Suspiciousness> result = new ArrayList<>();

        // 全クラス・全行を走査
        // ef, nf, ep, np を計算
        // suspiciousness を算出

        return result;
    }
}