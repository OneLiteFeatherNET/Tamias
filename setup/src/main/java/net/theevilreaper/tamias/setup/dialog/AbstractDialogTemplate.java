package net.theevilreaper.tamias.setup.dialog;


import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDialogTemplate<T> implements DialogTemplate<T> {

    protected final Component header;
    protected final Component submitComponent;
    protected final Component cancelComponent;

    protected AbstractDialogTemplate(
            @NotNull Component header,
            @NotNull Component submitComponent,
            @NotNull Component cancelComponent
    ) {
        this.header = header;
        this.submitComponent = submitComponent;
        this.cancelComponent = cancelComponent;
    }
}
