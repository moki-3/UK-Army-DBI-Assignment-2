
CREATE TABLE Branches (
    BranchID SERIAL PRIMARY KEY,
    BranchName VARCHAR(50) NOT NULL
);

INSERT INTO Branches (BranchName)
VALUES
('Army'),
('Navy'),
('Air Force'),
('Marines');

CREATE TABLE RankLevels (
    RankLevelID SERIAL PRIMARY KEY,
    LevelName VARCHAR(50) NOT NULL
);

INSERT INTO RankLevels (LevelName)
VALUES
('Mannschaft'),
('Unteroffizier'),
('Offizier'),
('Generalität');

CREATE TABLE NATOCodes (
    NATOCodeID SERIAL PRIMARY KEY,
    Code VARCHAR(5) NOT NULL
);

INSERT INTO NATOCodes (Code)
VALUES
('OR-1'), ('OR-4'), ('OR-5'),
('WO-1'),
('OF-1'), ('OF-2'), ('OF-3'), ('OF-4'), ('OF-5'),
('OF-7'), ('OF-8'), ('OF-9');

CREATE TABLE MilitaryRanks (
    RankID SERIAL PRIMARY KEY,
    RankName VARCHAR(50) NOT NULL,
    RankAbbreviation VARCHAR(10),
    BranchID INT REFERENCES Branches(BranchID),
    RankLevelID INT REFERENCES RankLevels(RankLevelID),
    NATOCodeID INT REFERENCES NATOCodes(NATOCodeID),
    SupervisorID INT REFERENCES MilitaryRanks(RankID)
);

INSERT INTO MilitaryRanks (RankName, RankAbbreviation, BranchID, RankLevelID, NATOCodeID, SupervisorID)
VALUES
('General', 'GEN', 1, 4, 12, NULL),
('Lieutenant General', 'LTG', 1, 4, 11, 1),
('Major General', 'MG', 1, 4, 10, 2),
('Colonel', 'COL', 1, 3, 9, 3),
('Lieutenant Colonel', 'LTC', 1, 3, 8, 4),
('Major', 'MAJ', 1, 3, 7, 5),
('Captain', 'CPT', 1, 3, 6, 6),
('Lieutenant', 'LT', 1, 3, 5, 7),
('Warrant Officer', 'WO', 1, 2, 4, 8),
('Sergeant', 'SGT', 1, 2, 3, 9),
('Corporal', 'CPL', 1, 2, 2, 10),
('Private', 'PVT', 1, 1, 1, 11);