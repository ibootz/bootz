package top.bootz.common.base;

import java.beans.Transient;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import top.bootz.common.utils.JsonUtil;
import top.bootz.common.utils.ToStringUtil;

public class BaseObject implements Serializable {

	private static final long serialVersionUID = 5836536587070114007L;

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
