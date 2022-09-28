package io.airbrake.javabrake;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Metrics {
    public static int FLUSH_PERIOD = 15;

    Date startTime = new Date();
    Date endTime;
    Map<String, Span> spans = new HashMap<>();
    Span currSpan;
    Map<String, Long> groups = new HashMap<>();

    public void end() {
        if (endTime == null)
            endTime = new Date();
    }

    protected Span newSpan(String name, Date startTime) {
        return new Span(this, name, startTime);
    }

    public void startSpan(String name, Date startTime) {
        if (this.currSpan != null) {
            if (this.currSpan.name == name) {
                this.currSpan.level += 1;
                return;
            }
            this.currSpan.pause();
        }

        Span span = this.spans.get(name);
        if (span == null) {
            span = this.newSpan(name, startTime);
            this.spans.put(name, span);
        } else
            span.resume();

        span.parent = this.currSpan;
        this.currSpan = span;
    }

    public void endSpan(String name, Date endTime) {
        if (this.currSpan != null && this.currSpan.name == name) {
            if (this._endSpan(this.currSpan, endTime)) {
                this.currSpan = this.currSpan.parent;
                if (this.currSpan != null)
                    this.currSpan.resume();
                return;
            }
        }

        Span span = this.spans.get(name);
        if (span == null)
            return;
        this._endSpan(span, endTime);
    }

    protected boolean _endSpan(Span span, Date endTime) {

        if (span.level > 0) {
            span.level -= 1;
            return false;
        }

        span.end(endTime);
        this.spans.get(span.name);
        return true;

    }

    protected void _inc_group(String name, long ms) {
        this.groups.put(name, (this.groups.getOrDefault(name, (long) 0) + ms));
    }
}

class Span {

    Metrics metric;
    Span parent;
    Date startTime;
    Date endTime;
    String name;
    long dur = 0;
    int level = 0;

    public Span(Metrics metric, String name, Date startTime) {
        this.metric = metric;
        this.startTime = startTime;
        this.name = name;
    }

    public void init() {
        this.startTime = new Date();
        this.endTime = null;
    }

    public void end(Date endTime) {
        if (endTime != null)
            this.endTime = endTime;
        else {
            this.endTime = new Date();
        }

        this.dur += (this.endTime.getTime() - this.metric.spans.get(this.name).startTime.getTime());
        this.metric._inc_group(this.name, this.dur);
        this.metric = null;
    }

    protected void pause() {
        if (this.paused())
            return;
        this.dur += (new Date().getTime() - this.startTime.getTime());
        this.startTime = null;
    }

    protected boolean paused() {
        return this.startTime == null;
    }

    protected void resume() {
        if (!this.paused())
            return;
        this.startTime = new Date();
    }
}
