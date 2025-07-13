    public boolean isResearchBlocked(String researchId) {
        return blockedResearches.contains(researchId);
    }
    
    public void blockResearch(String researchId) {
        blockedResearches.add(researchId);
    }
    
    public void unblockResearch(String researchId) {
        blockedResearches.remove(researchId);
    }