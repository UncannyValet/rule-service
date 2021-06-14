package com.example.rules.spi.arbiter;

import com.example.rules.api.RuleRequest;
import com.example.rules.api.RuleResult;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

public interface ArbiterRegistry {

    Class<? extends RuleResult> getResultClass(RuleRequest request);

    Class<? extends RuleResult> getResultClass(Class<? extends RuleRequest> requestClass);

    <R extends RuleRequest, A extends Arbiter<R, ? extends RuleResult>> A getArbiter(R request);
}
