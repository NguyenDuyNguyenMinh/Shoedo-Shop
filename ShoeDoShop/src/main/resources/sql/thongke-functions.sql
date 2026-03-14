-- ============================================================
-- SQL Functions for Statistics Date Filling
-- Run this script to create helper functions needed for statistics
-- ============================================================

USE ShoedoShop;
GO

-- ============================================================
-- Function: GetDatesBetween
-- Creates a table of all dates between start and end date
-- Used to fill missing dates in daily statistics with zero values
-- ============================================================
IF OBJECT_ID('dbo.GetDatesBetween', 'TF') IS NOT NULL
    DROP FUNCTION dbo.GetDatesBetween;
GO

CREATE FUNCTION dbo.GetDatesBetween
(
    @StartDate DATE,
    @EndDate DATE
)
RETURNS @Dates TABLE
(
    DateValue DATE PRIMARY KEY
)
AS
BEGIN
    DECLARE @CurrentDate DATE = @StartDate;
    
    WHILE @CurrentDate <= @EndDate
    BEGIN
        INSERT INTO @Dates (DateValue) VALUES (@CurrentDate);
        SET @CurrentDate = DATEADD(DAY, 1, @CurrentDate);
    END;
    
    RETURN;
END;
GO

-- ============================================================
-- Function: GetMonthsBetween
-- Creates a table of all months (YYYY-MM format) between start and end
-- Used to fill missing months in monthly statistics with zero values
-- ============================================================
IF OBJECT_ID('dbo.GetMonthsBetween', 'TF') IS NOT NULL
    DROP FUNCTION dbo.GetMonthsBetween;
GO

CREATE FUNCTION dbo.GetMonthsBetween
(
    @StartDate DATE,
    @EndDate DATE
)
RETURNS @Months TABLE
(
    MonthValue VARCHAR(7) PRIMARY KEY  -- Format: YYYY-MM
)
AS
BEGIN
    DECLARE @CurrentDate DATE = DATEFROMPARTS(YEAR(@StartDate), MONTH(@StartDate), 1);
    DECLARE @EndMonth DATE = DATEFROMPARTS(YEAR(@EndDate), MONTH(@EndDate), 1);
    
    WHILE @CurrentDate <= @EndMonth
    BEGIN
        INSERT INTO @Months (MonthValue) VALUES (FORMAT(@CurrentDate, 'yyyy-MM'));
        SET @CurrentDate = DATEADD(MONTH, 1, @CurrentDate);
    END;
    
    RETURN;
END;
GO

-- ============================================================
-- Test Examples
-- ============================================================

-- Test 1: Get all dates in a week
-- SELECT * FROM dbo.GetDatesBetween('2026-01-01', '2026-01-07');

-- Test 2: Get all months in a year
-- SELECT * FROM dbo.GetMonthsBetween('2026-01-01', '2026-12-31');

PRINT 'Functions created successfully!';
GO
