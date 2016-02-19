package io.corbel.iam.model;

import java.util.Date;
import java.util.Objects;

/**
 * A {@link TraceableEntity} is an entity which we can trace its creation.
 * 
 * @author Alexander De Leon
 * 
 */
public class TraceableEntity extends Entity {

    private Date createdDate;
    private String createdBy;

    public TraceableEntity(TraceableEntity traceableEntity) {
        super(traceableEntity);
        this.createdDate = traceableEntity.createdDate;
        this.createdBy = traceableEntity.createdBy;

    }

    public TraceableEntity() {
        super();
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (createdDate != null ? createdDate.hashCode() : 0);
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TraceableEntity)) {
            return false;
        }
        TraceableEntity that = (TraceableEntity) obj;
        return super.equals(that) && Objects.equals(this.createdBy, that.createdBy) && Objects.equals(this.createdDate, that.createdDate);
    }
}
