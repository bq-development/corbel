package io.corbel.iam.model;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class ScopeTest {

    private static Validator validator;

    @Before
    public void setUp(){
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void singleScopeBasicTest(){
        Scope s = getBasicScope();
        Scope verifiedScope = new Scope(s.getId(), null, s.getAudience(), s.getScopes(), s.getRules(), s.getParameters());
        assertTrue(validator.validate(verifiedScope).isEmpty());
    }

    @Test
    public void singleScopeBadAudienceTest(){
        Scope s = getBasicScope();
        Scope verifiedScope = new Scope(s.getId(), null, null, s.getScopes(), s.getRules(), s.getParameters());
        assertFalse(validator.validate(verifiedScope).isEmpty());
    }

    @Test
    public void singleScopeBadRulesTest(){
        Scope s = getBasicScope();
        Scope verifiedScope = new Scope(s.getId(), null, s.getAudience(), s.getScopes(), null, s.getParameters());
        assertFalse(validator.validate(verifiedScope).isEmpty());
    }

    private Scope getBasicScope(){
        Set<JsonObject> jsonObjectSet = new HashSet<>();
        jsonObjectSet.add(new JsonObject());
        return new Scope("ID", Scope.COMPOSITE_SCOPE_TYPE, "AUDIENCE", null, jsonObjectSet, new JsonObject());
    }
}
