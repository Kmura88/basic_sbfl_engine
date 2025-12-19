package basic_sbfl_engine.sbfl;

public class Ochiai extends SBFL {
	
    @Override
    protected double formula(double ef, double nf, double ep, double np) {
        if (ef == 0) return 0;
        return ef / Math.sqrt((ef + nf) * (ef + ep));
    }
}
