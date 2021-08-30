Consumer<List<Object>> applyEvent = new Consumer<List<Object>>() {

    @Override
    public void accept(List<Object> event) {
        Thread thread = Thread.currentThread();
        Map<Integer, List<List<Object>>> eventsMap = eventCollector.get(thread.getId());
        if (eventsMap == null) {
            eventsMap = new HashMap<Integer, List<Object>>();
            var eventList = new ArrayList<List<Object>>();
            eventList.add(event);
            eventsMap.put((Integer) event.get(0), eventList);
            eventCollector.put(thread.getId(), eventsMap);
        } else {
            eventsMap.get((Integer) event.get(0)).add(event);
        }
        threadCollector.put(thread.getId(), thread);
    }
};
