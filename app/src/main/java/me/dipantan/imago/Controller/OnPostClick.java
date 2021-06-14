package me.dipantan.imago.Controller;

import android.view.View;
import android.widget.CompoundButton;

public interface OnPostClick {
    void OnToggleClick(CompoundButton button, boolean checked, int position);

    void OnClick(View v, int position);
}
