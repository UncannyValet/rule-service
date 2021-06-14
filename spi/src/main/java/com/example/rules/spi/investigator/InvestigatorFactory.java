package com.example.rules.spi.investigator;

import com.example.rules.api.RuleRequest;
import com.example.rules.spi.Context;

import java.util.Collection;

public interface InvestigatorFactory {

    <R extends RuleRequest> Collection<Investigator<R, ?>> getInvestigators(Context context);
}
