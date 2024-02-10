package m4gshm.benchmark.rest.java.storage.model.impl;

import meta.Meta;
import meta.Meta.Extend;
import meta.Meta.Extend.Opt;
import meta.customizer.JpaColumns;

import javax.persistence.Column;
import javax.persistence.Id;

import static meta.customizer.JpaColumns.OPT_CLASS_NAME;

@Meta(customizers = @Extend(value = JpaColumns.class, opts = @Opt(key = OPT_CLASS_NAME, value = "TaskTagColumn")))
public record TaskTagImpl(@Id @Column(name = "TASK_ID") String taskId, String tag) {
}
