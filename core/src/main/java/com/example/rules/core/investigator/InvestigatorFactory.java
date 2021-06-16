package com.example.rules.core.investigator;

import com.example.rules.api.RuleRequest;
import com.example.rules.spi.RuleContext;
import com.example.rules.spi.investigator.Investigator;

import java.util.Collection;

public interface InvestigatorFactory {

    <R extends RuleRequest> Collection<Investigator<R, ?>> getInvestigators(RuleContext context);
}
