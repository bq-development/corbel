package com.bq.oss.corbel.event;

import com.bq.oss.corbel.eventbus.EventWithSpecificDomain;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class AssetsEvent extends EventWithSpecificDomain {
    private String userId;
    private List<EventAsset> assets;

    public AssetsEvent() {}

    public AssetsEvent(String domain, String userId, List<EventAsset> assets) {
        super(domain);
        this.userId = userId;
        this.assets = assets;
    }

    public String getUserId() {
        return userId;
    }

    public List<EventAsset> getAssets() {
        return assets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AssetsEvent))
            return false;
        if (!super.equals(o))
            return false;

        AssetsEvent that = (AssetsEvent) o;

        if (assets != null ? !assets.equals(that.assets) : that.assets != null)
            return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (assets != null ? assets.hashCode() : 0);
        return result;
    }

    public static class EventAsset {
        private Set<String> scopes;
        private Date expire;
        private String name;
        private String productId;

        public EventAsset() {}

        public EventAsset(Set<String> scopes, Date expire, String name, String productId) {
            super();
            this.scopes = scopes;
            this.expire = expire;
            this.name = name;
            this.productId = productId;
        }

        public Set<String> getScopes() {
            return scopes;
        }

        public Date getExpire() {
            return expire;
        }

        public String getName() {
            return name;
        }

        public String getProductId() {
            return productId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof EventAsset))
                return false;

            EventAsset that = (EventAsset) o;

            if (expire != null ? !expire.equals(that.expire) : that.expire != null)
                return false;
            if (name != null ? !name.equals(that.name) : that.name != null)
                return false;
            if (productId != null ? !productId.equals(that.productId) : that.productId != null)
                return false;
            if (scopes != null ? !scopes.equals(that.scopes) : that.scopes != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = scopes != null ? scopes.hashCode() : 0;
            result = 31 * result + (expire != null ? expire.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (productId != null ? productId.hashCode() : 0);
            return result;
        }

    }

}
