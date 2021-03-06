package edu.gemini.spModel.gemini.gnirs;

import edu.gemini.pot.sp.ISPSeqComponent;

import edu.gemini.spModel.obscomp.InstConstants;
import edu.gemini.spModel.seqcomp.SeqConfigNames;
import edu.gemini.spModel.config.HelperSeqCompCB;
import edu.gemini.spModel.data.config.StringParameter;
import edu.gemini.spModel.data.config.IConfig;
import edu.gemini.spModel.data.config.IParameter;
import edu.gemini.spModel.data.config.ISysConfig;

/**
 * A configuration builder for the GNIRS iterator.
 */
public final class SeqConfigGNIRSCB extends HelperSeqCompCB {

    /**
     * Constructor for creating this seq comp CB.
     */
    public SeqConfigGNIRSCB(ISPSeqComponent seqComp) {
        super(seqComp);
    }


    /**
     * This thisApplyNext overrides the HelperSeqCompCB
     * so that the integration time, exposure time and ncoadds can
     * be inserting in the observe system.
     */
    protected void thisApplyNext(IConfig config, IConfig prevFull) {
        super.thisApplyNext(config, prevFull);

        // Insert the instrument name
        config.putParameter(SeqConfigNames.INSTRUMENT_CONFIG_NAME,
                StringParameter.getInstance(InstConstants.INSTRUMENT_NAME_PROP, GNIRSConstants.INSTRUMENT_NAME_PROP));

        ISysConfig sysConfig = config.getSysConfig(SeqConfigNames.INSTRUMENT_CONFIG_NAME);
        for (IParameter param : sysConfig.getParameters()) {
            String name = param.getName();

            if ((name != null) && name.equals(GNIRSConstants.CENTRAL_WAVELENGTH_PROP)) {
                GNIRSParams.Wavelength w = (GNIRSParams.Wavelength) param.getValue();
                String obsWave = (w == null) ? "" : w.getStringValue();
                config.putParameter(SeqConfigNames.INSTRUMENT_CONFIG_NAME,
                        StringParameter.getInstance(GNIRSConstants.OBSERVING_WAVELENGTH_PROP, obsWave));
            }
        }
    }
}
