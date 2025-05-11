package m4gshm.benchmark.rest.java.storage.model.impl;

import meta.Meta;
import meta.Meta.Extend;
import meta.Meta.Extend.Opt;
import meta.jpa.customizer.JpaColumns;

import javax.persistence.Id;

import static meta.jpa.customizer.JpaColumns.OPT_CLASS_NAME;
import static meta.jpa.customizer.JpaColumns.OPT_GENERATED_COLUMN_NAME_POST_PROCESS;

@Meta(customizers = @Extend(value = JpaColumns.class, opts = {
        @Opt(key = OPT_CLASS_NAME, value = "TaskTagColumn"),
        @Opt(key = OPT_GENERATED_COLUMN_NAME_POST_PROCESS, value = "toLowerCase"),
}))
public record TaskTagImpl(@Id String taskId, String tag) {
}
