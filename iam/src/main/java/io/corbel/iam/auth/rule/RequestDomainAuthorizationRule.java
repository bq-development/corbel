package io.corbel.iam.auth.rule;

import java.util.Objects;
import java.util.Optional;

import io.corbel.iam.auth.AuthorizationRequestContext;
import io.corbel.iam.auth.AuthorizationRule;
import io.corbel.iam.exception.UnauthorizedException;
import io.corbel.iam.model.Domain;
import io.corbel.iam.utils.Message;

public class RequestDomainAuthorizationRule implements AuthorizationRule {

    @Override
    public void process(AuthorizationRequestContext context) throws UnauthorizedException {
        if (isCrossDomain(context) && !isAllowedClientCrossDomain(context) && !isAllowedChildCrossDomain(context)) {
            throw new UnauthorizedException(Message.REQUESTED_DOMAIN_NOT_ALLOWED.getMessage());
        }
    }

    private boolean isCrossDomain(AuthorizationRequestContext context) {
        return !Objects.equals(context.getRequestedDomain().getId(), context.getIssuerClientDomain().getId());
    }

    private boolean isAllowedChildCrossDomain(AuthorizationRequestContext context) {
        return context.getRequestedDomain().getId().matches(getChildrenDomainRegex(context));
    }

    private boolean isAllowedClientCrossDomain(AuthorizationRequestContext context) {
        return Optional.ofNullable(context.getIssuerClientDomain().getAllowedDomains())
                .map(allowedDomains -> context.getRequestedDomain().getId().matches(allowedDomains)).orElse(false);
    }

    private String getChildrenDomainRegex(AuthorizationRequestContext context) {
        return context.getIssuerClientDomain().getId() + "(" + Domain.ID_SEPARATOR + ".+" + ")";
    }

}
