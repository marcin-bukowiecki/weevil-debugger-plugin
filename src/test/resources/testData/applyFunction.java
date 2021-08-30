class ApplyFunction {

    public void test() {
        Function<List<Object>, Object> applyFunction = event -> {
            Thread thread = Thread.currentThread();
            Map<Integer, List<List<Object>>> threadEvents = eventCollector.get(thread.getId());
            if (threadEvents == null) {
                var eventsMap = new HashMap<Integer, List<List<Object>>>();
                var eventList = new ArrayList<List<Object>>();
                eventList.add(event);
                eventsMap.put((Integer) event.get(0), eventList);
                eventCollector.put(thread.getId(), eventsMap);
            } else {
                var eventsMap = threadEvents.get((Integer) event.get(0));
                if (eventsMap == null) {
                    var eventList = new ArrayList<List<Object>>();
                    eventList.add(event);
                    eventCollector.get(thread.getId()).put((Integer) event.get(0), eventList);
                } else {
                    eventsMap.add(event);
                }
            }
            threadCollector.put(thread.getId(), thread);
            return event.get(1);
        };
    }
}