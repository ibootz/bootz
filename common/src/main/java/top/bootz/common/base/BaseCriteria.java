package top.bootz.common.base;

import java.beans.Transient;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import top.bootz.common.utils.JsonUtil;
import top.bootz.common.utils.ToStringUtil;

public class BaseCriteria implements Serializable {

	private static final long serialVersionUID = 8591142014745925855L;

	@Transient
	@JsonIgnore
	@Override
	public String toString() {
		return ToStringUtil.toJSON(this);
	}

	@Transient
	@JsonIgnore
	public String toJson() {
		return JsonUtil.toJSON(this);
	}

}
