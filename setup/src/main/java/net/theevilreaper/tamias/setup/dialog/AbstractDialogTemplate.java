package net.theevilreaper.tamias.setup.dialog;

import net.kyori.adventure.text.Component;

public abstract class AbstractDialogTemplate<T> implements DialogTemplate<T> {

    protected final Component header;
    protected final Component submitComponent;
    protected final Component cancelComponent;

    protected AbstractDialogTemplate(
            Component header,
            Component submitComponent,
            Component cancelComponent
    ) {
        this.header = header;
        this.submitComponent = submitComponent;
        this.cancelComponent = cancelComponent;
    }
}
