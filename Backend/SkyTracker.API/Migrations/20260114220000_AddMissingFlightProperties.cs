using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace SkyTracker.API.Migrations
{
    /// <inheritdoc />
    public partial class AddMissingFlightProperties : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.RenameColumn(
                name: "LastUpdated",
                table: "Flights",
                newName: "LastSeen");

            migrationBuilder.AddColumn<string>(
                name: "Country",
                table: "Flights",
                type: "text",
                nullable: false,
                defaultValue: "");

            migrationBuilder.AddColumn<DateTime>(
                name: "FirstSeen",
                table: "Flights",
                type: "timestamp with time zone",
                nullable: false,
                defaultValue: new DateTime(1, 1, 1, 0, 0, 0, 0, DateTimeKind.Unspecified));

            migrationBuilder.AddColumn<DateTime>(
                name: "LastPositionUpdate",
                table: "Flights",
                type: "timestamp with time zone",
                nullable: false,
                defaultValue: new DateTime(1, 1, 1, 0, 0, 0, 0, DateTimeKind.Unspecified));
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "Country",
                table: "Flights");

            migrationBuilder.DropColumn(
                name: "FirstSeen",
                table: "Flights");

            migrationBuilder.DropColumn(
                name: "LastPositionUpdate",
                table: "Flights");

            migrationBuilder.RenameColumn(
                name: "LastSeen",
                table: "Flights",
                newName: "LastUpdated");
        }
    }
}
